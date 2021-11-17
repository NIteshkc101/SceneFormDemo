package com.example.augmentedreality

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment.OnTapArPlaneListener
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*
import java.util.function.Consumer
import java.util.function.Function


class MainActivity : AppCompatActivity() {
    private var arCam //object of ArFragment Class
            : ArFragment? = null
    private var clickNo = 0 //helps to render the 3d model only once when we tap the screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkSystemSupport(this)) {
            arCam = supportFragmentManager.findFragmentById(R.id.arCameraArea) as ArFragment?
            //ArFragment is linked up with its respective id used in the activity_main.xml
            arCam!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
                clickNo++
                //the 3d model comes to the scene only when clickNo is one that means once
                if (clickNo == 1) {
                    val anchor = hitResult.createAnchor()
                    ModelRenderable.builder()
                        .setSource(this, R.raw.ball)
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(Consumer { modelRenderable: ModelRenderable ->
                            addModel(
                                anchor,
                                modelRenderable
                            )
                        })
                        .exceptionally(Function<Throwable, Void?> { throwable: Throwable ->
                            val builder =
                                AlertDialog.Builder(this)
                            builder.setMessage("Somthing is not right" + throwable.message).show()
                            null
                        })
                }
            }
        } else {
            return
        }
    }

    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        // Creating a AnchorNode with a specific anchor
        anchorNode.setParent(arCam!!.arSceneView.scene)
        //attaching the anchorNode with the ArFragment
        val model = TransformableNode(arCam!!.transformationSystem)
        model.setParent(anchorNode)
        //attaching the anchorNode with the TransformableNode
        model.renderable = modelRenderable
        //attaching the 3d model with the TransformableNode that is already attached with the node
        model.select()
    }

    companion object {
        fun checkSystemSupport(activity: Activity): Boolean {

            //checking whether the API version of the running Android >= 24 that means Android Nougat 7.0
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val openGlVersion = (Objects.requireNonNull(
                    activity.getSystemService(
                        ACTIVITY_SERVICE
                    )
                ) as ActivityManager).deviceConfigurationInfo.glEsVersion

                //checking whether the OpenGL version >= 3.0
                if (openGlVersion.toDouble() >= 3.0) {
                    true
                } else {
                    Toast.makeText(
                        activity,
                        "App needs OpenGl Version 3.0 or later",
                        Toast.LENGTH_SHORT
                    ).show()
                    activity.finish()
                    false
                }
            } else {
                Toast.makeText(
                    activity,
                    "App does not support required Build Version",
                    Toast.LENGTH_SHORT
                ).show()
                activity.finish()
                false
            }
        }
    }
}