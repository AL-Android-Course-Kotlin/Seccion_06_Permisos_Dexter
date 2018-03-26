package com.alejandrolora.seccion_06_permisos_dexter.activities

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.alejandrolora.seccion_06_permisos_dexter.R
import com.alejandrolora.seccion_06_permisos_dexter.enums.PermissionStatusEnum
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setButtonClicks()
    }

    private fun setButtonClicks() {
        buttonCamera.setOnClickListener { checkCameraPermissions() }
        buttonContacts.setOnClickListener { checkContactsPermissions() }
        buttonAudio.setOnClickListener { checkAudioPermissions() }
        buttonAll.setOnClickListener { checkAllPermissions() }
    }

//    private fun checkCameraPermissions() = setPermissionHandler(Manifest.permission.CAMERA, textViewCamera)

//    private fun checkCameraPermissions() = setCameraPermissionHandlerWithDialog()

    private fun checkCameraPermissions() = setCameraPermissionHandlerWithSnackbar()

    private fun checkContactsPermissions() = setPermissionHandler(Manifest.permission.READ_CONTACTS, textViewContacts)

    private fun checkAudioPermissions() = setPermissionHandler(Manifest.permission.RECORD_AUDIO, textViewAudio)

    private fun checkAllPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            for (permission in report.grantedPermissionResponses) {
                                when (permission.permissionName) {
                                    Manifest.permission.CAMERA -> setPermissionStatus(textViewCamera, PermissionStatusEnum.GRANTED)
                                    Manifest.permission.READ_CONTACTS -> setPermissionStatus(textViewContacts, PermissionStatusEnum.GRANTED)
                                    Manifest.permission.RECORD_AUDIO -> setPermissionStatus(textViewAudio, PermissionStatusEnum.GRANTED)
                                }
                            }
                            for (permission in report.deniedPermissionResponses) {
                                when (permission.permissionName) {
                                    Manifest.permission.CAMERA -> {
                                        if (permission.isPermanentlyDenied) {
                                            setPermissionStatus(textViewCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                                        } else {
                                            setPermissionStatus(textViewCamera, PermissionStatusEnum.DENIED)
                                        }
                                    }
                                    Manifest.permission.READ_CONTACTS -> {
                                        if (permission.isPermanentlyDenied) {
                                            setPermissionStatus(textViewContacts, PermissionStatusEnum.PERMANENTLY_DENIED)
                                        } else {
                                            setPermissionStatus(textViewContacts, PermissionStatusEnum.DENIED)
                                        }
                                    }
                                    Manifest.permission.RECORD_AUDIO -> {
                                        if (permission.isPermanentlyDenied) {
                                            setPermissionStatus(textViewAudio, PermissionStatusEnum.PERMANENTLY_DENIED)
                                        } else {
                                            setPermissionStatus(textViewAudio, PermissionStatusEnum.DENIED)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                }).check()
    }

    private fun setPermissionHandler(permission: String, textView: TextView) {
        Dexter.withActivity(this)
                .withPermission(permission)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        setPermissionStatus(textView, PermissionStatusEnum.GRANTED)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            setPermissionStatus(textView, PermissionStatusEnum.PERMANENTLY_DENIED)
                        } else {
                            setPermissionStatus(textView, PermissionStatusEnum.DENIED)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    private fun setPermissionStatus(textView: TextView, status: PermissionStatusEnum) {
        when (status) {
            PermissionStatusEnum.GRANTED -> {
                textView.text = getString(R.string.permission_status_granted)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusGranted))
            }
            PermissionStatusEnum.DENIED -> {
                textView.text = getString(R.string.permission_status_denied)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusDenied))
            }
            PermissionStatusEnum.PERMANENTLY_DENIED -> {
                textView.text = getString(R.string.permission_status_denied_permanently)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusPermanentlyDenied))
            }
        }
    }

    private fun setCameraPermissionHandlerWithDialog() {
        val dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                .withContext(this)
                .withTitle("Camera Permission")
                .withMessage("Camera permission is needed to take pictures")
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build()

        val permission = object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                setPermissionStatus(textViewCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                if (response.isPermanentlyDenied) {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        }

        val composite = CompositePermissionListener(permission, dialogPermissionListener)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(composite)
                .check()
    }

    private fun setCameraPermissionHandlerWithSnackbar() {

        val snackbarPermissionListener = SnackbarOnDeniedPermissionListener.Builder
                .with(root, "Camera is needed to take pictures")
                .withOpenSettingsButton("Settings")
                .withCallback(object : Snackbar.Callback() {
                    override fun onShown(sb: Snackbar?) {
                        // Event handler for when the given Snackbar is visible
                    }
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        // Event handler for when the given Snackbar has been dismissed
                    }
                }).build()


        val permission = object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                setPermissionStatus(textViewCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                if (response.isPermanentlyDenied) {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        }

        val composite = CompositePermissionListener(permission, snackbarPermissionListener)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(composite)
                .check()

    }
}
