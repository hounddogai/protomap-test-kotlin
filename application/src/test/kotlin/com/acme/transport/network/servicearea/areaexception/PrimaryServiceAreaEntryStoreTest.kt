package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryServiceAreaEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryServiceAreaEntryStore()
        val item = newPrimaryServiceAreaEntry("code-962", "label-673")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
