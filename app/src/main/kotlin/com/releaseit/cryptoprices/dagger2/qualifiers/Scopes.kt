package com.releaseit.rxrepository.dagger2.qualifiers

import javax.inject.Scope

/**
 * Created by jurajbegovac on 27/01/2018.
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerFragment
