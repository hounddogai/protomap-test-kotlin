package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingEmissionFactorQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingEmissionFactorQuotaStore()
        val item = newRollingEmissionFactorQuota("code-775", "name-269")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
