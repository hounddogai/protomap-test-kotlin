package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalServiceAreaLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalServiceAreaLedgerEngine(newRegistry())
        val item = newSeasonalServiceAreaLedger("code-850", "title-878")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
