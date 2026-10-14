package com.acme.transport.customs.clearance.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientClearanceWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientClearanceWindowStore()
        val item = newLenientClearanceWindow("code-930", "label-152")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
