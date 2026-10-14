package com.acme.transport.linehaul.laneplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLanePlanQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLanePlanQuotaStore()
        val item = newScheduledLanePlanQuota("code-305", "label-264")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
