package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaSummaryStore()
        val item = newServiceAreaSummary("code-784", "name-989")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
