package com.acme.transport.customs.hold.holdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualHoldProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualHoldProfileStore()
        val item = newManualHoldProfile("code-914", "label-216")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
