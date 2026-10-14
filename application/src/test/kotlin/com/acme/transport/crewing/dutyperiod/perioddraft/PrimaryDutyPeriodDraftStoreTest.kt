package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDutyPeriodDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDutyPeriodDraftStore()
        val item = newPrimaryDutyPeriodDraft("code-684", "label-379")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
