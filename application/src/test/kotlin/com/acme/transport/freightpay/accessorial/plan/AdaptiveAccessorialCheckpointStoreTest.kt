package com.acme.transport.freightpay.accessorial.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAccessorialCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAccessorialCheckpointStore()
        val item = newAdaptiveAccessorialCheckpoint("code-370", "title-62")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
