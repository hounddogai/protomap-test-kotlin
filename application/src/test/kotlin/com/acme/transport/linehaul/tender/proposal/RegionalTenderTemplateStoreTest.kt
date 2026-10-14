package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalTenderTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalTenderTemplateStore()
        val item = newRegionalTenderTemplate("code-654", "title-22")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
