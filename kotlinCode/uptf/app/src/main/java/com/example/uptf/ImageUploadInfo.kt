package com.example.uptf

class ImageUploadInfo {
    var imageName: String? = null
    var imageURL: String? = null

    constructor() {}
    constructor(name: String?, url: String?) {
        imageName = name
        imageURL = url
    }

}