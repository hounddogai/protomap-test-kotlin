package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTenderRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTenderRollupStore()
        val item = newPrimaryTenderRollup("code-868", "name-811")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
