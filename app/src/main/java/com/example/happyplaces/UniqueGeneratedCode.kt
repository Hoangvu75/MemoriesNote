package com.example.happyplaces

import com.chaquo.python.Python

val obj = Python.getInstance().getModule("PyScript").callAttr("read_code").toString()


