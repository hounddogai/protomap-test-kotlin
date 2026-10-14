package com.acme.transport.linehaul.laneplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLanePlanLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLanePlanLedgerStore()
        val item = newRollingLanePlanLedger("code-530", "title-304")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
