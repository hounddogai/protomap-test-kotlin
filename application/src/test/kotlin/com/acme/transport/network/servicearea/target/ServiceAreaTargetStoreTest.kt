package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaTargetStore()
        val item = newServiceAreaTarget("code-202", "title-656")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
