package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCameraEventTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCameraEventTargetStore()
        val item = newPrimaryCameraEventTarget("code-11", "title-96")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
