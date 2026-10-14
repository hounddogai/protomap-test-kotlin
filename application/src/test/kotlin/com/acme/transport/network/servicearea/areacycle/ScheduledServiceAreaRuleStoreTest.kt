package com.acme.transport.network.servicearea.areacycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledServiceAreaRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledServiceAreaRuleStore()
        val item = newScheduledServiceAreaRule("code-414", "name-505")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
