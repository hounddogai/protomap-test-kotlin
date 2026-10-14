package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventAgreementStore()
        val item = newCameraEventAgreement("code-335", "label-505")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
