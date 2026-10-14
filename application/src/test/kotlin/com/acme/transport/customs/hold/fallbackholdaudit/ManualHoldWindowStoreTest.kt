package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualHoldWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualHoldWindowStore()
        val item = newManualHoldWindow("code-13", "name-767")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
