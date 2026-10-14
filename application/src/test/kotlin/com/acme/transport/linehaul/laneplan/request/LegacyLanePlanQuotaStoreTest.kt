package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyLanePlanQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyLanePlanQuotaStore()
        val item = newLegacyLanePlanQuota("code-329", "title-877")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
