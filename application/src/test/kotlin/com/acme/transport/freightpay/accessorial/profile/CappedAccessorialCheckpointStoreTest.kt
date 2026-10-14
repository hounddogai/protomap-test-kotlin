package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAccessorialCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAccessorialCheckpointStore()
        val item = newCappedAccessorialCheckpoint("code-590", "name-747")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
