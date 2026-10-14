package com.acme.transport.linehaul.backhaul.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalBackhaulBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalBackhaulBaselineStore()
        val item = newRegionalBackhaulBaseline("code-682", "title-135")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
