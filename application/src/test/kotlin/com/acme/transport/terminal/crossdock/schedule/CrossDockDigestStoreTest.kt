package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockDigestStore()
        val item = newCrossDockDigest("code-572", "title-879")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
