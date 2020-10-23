package io.nodle.substratesdk.utils

/**
 * @author Lucien Loiseau on 14/07/20.
 */
fun onDebugOnly(job: () -> Unit) {
    job()
}
