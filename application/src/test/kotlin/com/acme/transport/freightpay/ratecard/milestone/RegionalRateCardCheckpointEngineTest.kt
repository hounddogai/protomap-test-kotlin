package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRateCardCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRateCardCheckpointEngine(newRegistry())
        val item = newRegionalRateCardCheckpoint("code-363", "title-238")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
