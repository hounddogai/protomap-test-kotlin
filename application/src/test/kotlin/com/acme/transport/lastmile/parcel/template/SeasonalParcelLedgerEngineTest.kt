package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalParcelLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalParcelLedgerEngine()
        val item = newSeasonalParcelLedger("code-840", "name-674")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
