package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventBudgetStore()
        val item = newCameraEventBudget("code-985", "label-566")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
