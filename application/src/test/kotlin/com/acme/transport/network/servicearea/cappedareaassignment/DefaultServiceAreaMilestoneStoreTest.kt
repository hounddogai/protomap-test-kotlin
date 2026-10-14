package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultServiceAreaMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultServiceAreaMilestoneStore()
        val item = newDefaultServiceAreaMilestone("code-305", "title-942")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
