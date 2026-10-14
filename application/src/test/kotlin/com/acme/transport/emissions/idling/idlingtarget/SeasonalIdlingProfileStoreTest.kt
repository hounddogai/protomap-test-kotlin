package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalIdlingProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalIdlingProfileStore()
        val item = newSeasonalIdlingProfile("code-544", "label-242")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
