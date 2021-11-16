package com.example.team_project_0_posting

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyGlideModule : AppGlideModule() {
    // Glide 대신 GlideApp을 써야

    // Failed to find GeneratedAppGlideModule. You should include an annotationProcessor compile dependency
    // on com.github.bumptech.glide:compiler in your application
    // and a @GlideModule annotated AppGlideModule implementation or LibraryGlideModules will be silently ignored

    // 뭐 이런 오류가 안 뜸.
}