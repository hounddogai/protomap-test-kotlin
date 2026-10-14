package com.acme.transport.customs.hold.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientHoldGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientHoldGroupStore()
        val item = newLenientHoldGroup("code-421", "label-32")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
