package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAuditScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAuditScheduleStore()
        val item = newSeasonalAuditSchedule("code-221", "title-6")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
