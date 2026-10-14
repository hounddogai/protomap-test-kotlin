package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyBackhaulQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyBackhaulQuotaStore()
        val item = newLegacyBackhaulQuota("code-546", "title-229")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
