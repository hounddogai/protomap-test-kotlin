package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDutyPeriodProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDutyPeriodProfileStore()
        val item = newLenientDutyPeriodProfile("code-500", "name-862")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
