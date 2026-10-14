package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaAdjustmentStore()
        val item = newServiceAreaAdjustment("code-133", "title-578")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
