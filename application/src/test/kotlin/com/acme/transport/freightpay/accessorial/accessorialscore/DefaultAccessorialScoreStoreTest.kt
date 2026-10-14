package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAccessorialScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultAccessorialScoreStore()
        val item = newDefaultAccessorialScore("code-243", "label-136")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
