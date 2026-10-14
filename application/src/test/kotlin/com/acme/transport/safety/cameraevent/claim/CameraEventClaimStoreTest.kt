package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventClaimStore()
        val item = newCameraEventClaim("code-266", "label-952")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
