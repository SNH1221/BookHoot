package com.skyrist.bookhub

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pManager.ServiceResponseListener
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import util.ConnectionManager
import java.util.Collections

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    lateinit var recyclerDashboard : RecyclerView

    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var  recyclerAdapter: DashboardRecyclerAdapter

    lateinit var bookInfoList : ArrayList<Book>

    lateinit var progressLayout : RelativeLayout

    lateinit var progressBar : ProgressBar

    var ratingComparator = Comparator<Book>{book1, book2 ->

        if(book1.bookRating.compareTo(book2.bookRating, true) == 0){
            //sort according to name if rating is same
            book1.bookName.compareTo(book2.bookName, true)

        } else {
            book1.bookRating.compareTo(book2.bookRating, true)
        }


    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)

        bookInfoList = ArrayList()

        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)

        progressLayout = view.findViewById(R.id.progressLayout)

        progressBar = view.findViewById(R.id.progressBar)

        progressLayout.visibility = View.VISIBLE


        layoutManager = LinearLayoutManager(activity)

        val queue = Volley.newRequestQueue(activity as Context)

        val url = "Your_URL"


        if (ConnectionManager().checkConnectivity(activity as Context)){

            val jsonObjectRequest = object : JsonObjectRequest (Request.Method.GET, url, null, Response.Listener {



                // Here we will handle response

                try {

                    progressLayout.visibility = View.GONE

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONObject("data")
                        val keys = data.keys()
                        while (keys.hasNext()){
                            val key = keys.next()
                            val bookJsonObject = data.getJSONObject(key)
                            val bookObject = Book (
                                bookJsonObject.getString("book_id"),
                                bookJsonObject.getString("name"),
                                bookJsonObject.getString("author"),
                                bookJsonObject.getString("rating"),
                                bookJsonObject.getString("price"),
                                bookJsonObject.getString("image")

                            )
                            bookInfoList.add(bookObject)

                        }
                        recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)

                        recyclerDashboard.adapter = recyclerAdapter

                        recyclerDashboard.layoutManager = layoutManager


                    }else{
                        Toast.makeText(activity as Context, "Some Error Occurred!!!",Toast.LENGTH_SHORT).show()

                    }


                } catch (e: JSONException){

                    Toast.makeText(activity as Context, "Some unexpected error occurred !!!", Toast.LENGTH_SHORT).show()
                }


            },
                Response.ErrorListener {

                    // Here we will handle errors
                    if (activity != null){

                        progressLayout.visibility = View.GONE

                        val dialog = AlertDialog.Builder(activity as Context)
                        dialog.setTitle("Server Cold")
                        dialog.setMessage("Server may be waking up. Please try again.")

                        dialog.setPositiveButton("Retry") { _, _ ->
                            val retryRequest = object : JsonObjectRequest(
                                Request.Method.GET,
                                url,
                                null,
                                Response.Listener { response ->
                                    progressLayout.visibility = View.GONE

                                    val success = response.getBoolean("success")
                                    if (success) {
                                        val data = response.getJSONObject("data")
                                        bookInfoList.clear()
                                        val keys = data.keys()
                                        while (keys.hasNext()) {
                                            val key = keys.next()
                                            val bookJsonObject = data.getJSONObject(key)
                                            val bookObject = Book(
                                                bookJsonObject.getString("book_id"),
                                                bookJsonObject.getString("name"),
                                                bookJsonObject.getString("author"),
                                                bookJsonObject.getString("rating"),
                                                bookJsonObject.getString("price"),
                                                bookJsonObject.getString("image")
                                            )
                                            bookInfoList.add(bookObject)
                                        }

                                        recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)
                                        recyclerDashboard.adapter = recyclerAdapter
                                        recyclerDashboard.layoutManager = layoutManager
                                    }
                                },
                                Response.ErrorListener {
                                    Toast.makeText(context, "Still not responding, try again later.", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                override fun getHeaders(): MutableMap<String, String> {
                                    val headers = HashMap<String, String>()
                                    headers["Content-type"] = "application/json"
                                    headers["token"] = "your_token_here" // replace with your token
                                    return headers
                                }
                            }

                            queue.add(retryRequest)
                            progressLayout.visibility = View.VISIBLE
                        }

                        dialog.setNegativeButton("Exit") { _, _ ->
                            ActivityCompat.finishAffinity(activity as Activity)
                        }

                        dialog.create().show()

                    }


                }) {

                override fun getHeaders() : MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "Your_Token"
                    return headers
                }

            }

            queue.add(jsonObjectRequest)

        } else{

            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Not Connection Found ")
            dialog.setPositiveButton("Open Settings") {text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()

            }

            dialog.setNegativeButton("Exit"){text, listener ->

                ActivityCompat.finishAffinity(activity as Activity)


            }
            dialog.create()
            dialog.show()

        }



        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item?.itemId
        if(id == R.id.action_sort){
            Collections.sort(bookInfoList, ratingComparator)
            bookInfoList.reverse()
        }

        recyclerAdapter.notifyDataSetChanged()






        return super.onOptionsItemSelected(item)
    }


}
