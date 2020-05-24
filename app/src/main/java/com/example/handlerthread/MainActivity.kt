package com.example.handlerthread

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var items: MutableList<String> = mutableListOf();

    private lateinit var handlerThread: HandlerThread
    private lateinit var threadHandler: Handler
    private val uiHandler = UIHandler(this)

    class UIHandler(activity: MainActivity): Handler() {

        private val mActivity: WeakReference<MainActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity = mActivity.get()
            activity?.addItems(msg.obj as String)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()
        initHandlerThread()
        bind()
    }

    private fun bind() {
        download_button.setOnClickListener {
            if (handlerThread.isAlive) {
                val message = Message()
                message.obj = arrayOf("image1", "image2", "image3", "image4")
                threadHandler.sendMessage(message)
            }
        }

        stop_button.setOnClickListener {
            handlerThread.quitSafely()
        }

        restart_button.setOnClickListener {
            if (!handlerThread.isAlive) {
                initHandlerThread()
            }
        }
    }

    private fun initHandlerThread () {
        handlerThread = HandlerThread("ImageLoader")
        handlerThread.start()

        threadHandler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val urls = msg.obj as Array<String>
                urls.forEach {

                    val message = Message()
                    message.obj = "$it download success"
                    uiHandler.sendMessage(message)

                    try {
                        Thread.sleep(1000)
                    }  catch (exception: InterruptedException) {
                        exception.printStackTrace()
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = RecyclerAdapter(items)

        recyclerView =  recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }


    private fun addItems(result: String) {
        items.add(result)
        viewAdapter.notifyDataSetChanged();
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
        uiHandler.removeCallbacksAndMessages(null);
    }
}
