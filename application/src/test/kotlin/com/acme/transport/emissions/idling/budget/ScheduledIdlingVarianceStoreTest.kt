package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledIdlingVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledIdlingVarianceStore()
        val item = newScheduledIdlingVariance("code-826", "title-622")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
