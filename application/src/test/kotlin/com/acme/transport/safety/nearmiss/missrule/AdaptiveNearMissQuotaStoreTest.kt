package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveNearMissQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveNearMissQuotaStore()
        val item = newAdaptiveNearMissQuota("code-294", "label-612")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
