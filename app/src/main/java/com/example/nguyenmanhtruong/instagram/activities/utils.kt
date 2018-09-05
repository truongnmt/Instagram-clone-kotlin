package com.example.nguyenmanhtruong.instagram.activities

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.nguyenmanhtruong.instagram.R
import com.example.nguyenmanhtruong.instagram.models.User
import com.example.nguyenmanhtruong.instagram.utils.GlideApp
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun coordinateBtnAndInputs(btn: Button, vararg inputs: EditText) {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            btn.isEnabled = inputs.all { it.text.isNotEmpty() }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.isNotEmpty() }
}

fun ImageView.loadUserPhoto(photoUrl: String?) {
//    if (!(context as Activity).isDestroyed) {
    GlideApp.with(this).load(photoUrl).fallback(R.drawable.person).into(this)
}

fun Editable.toStringOrNull(): String? {
    val str = toString()
    return if (str.isEmpty()) null else str
}

fun ImageView.loadImage(image: String) {
    GlideApp.with(this).load(image).centerCrop().into(this)
}

fun <T> task(block: (TaskCompletionSource<T>) -> Unit) : Task<T> {
    val taskSource = TaskCompletionSource<T>()
    block(taskSource)
    return taskSource.task
}

fun DataSnapshot.asUser(): User? =
        getValue(User::class.java)?.copy(uid = key)