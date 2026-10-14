package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCameraEventScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCameraEventScoreStore()
        val item = newDefaultCameraEventScore("code-441", "label-60")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
