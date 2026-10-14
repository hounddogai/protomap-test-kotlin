package com.acme.transport.terminal.dwell.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDwellProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDwellProfileStore()
        val item = newSeasonalDwellProfile("code-494", "label-224")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
