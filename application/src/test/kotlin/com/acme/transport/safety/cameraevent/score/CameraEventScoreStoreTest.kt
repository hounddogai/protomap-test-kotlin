package com.acme.transport.safety.cameraevent.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventScoreStore()
        val item = newCameraEventScore("code-754", "name-539")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
