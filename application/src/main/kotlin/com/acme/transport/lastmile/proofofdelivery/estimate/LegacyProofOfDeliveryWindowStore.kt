package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy proof of delivery windows by code. */
interface LegacyProofOfDeliveryWindowStore {
    fun find(code: String): LegacyProofOfDeliveryWindow?
    fun save(item: LegacyProofOfDeliveryWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyProofOfDeliveryWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyProofOfDeliveryWindowStore(
    var items: MutableMap<String, LegacyProofOfDeliveryWindow>,
    var order: MutableList<String>,
) : LegacyProofOfDeliveryWindowStore {
    override fun find(code: String): LegacyProofOfDeliveryWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyProofOfDeliveryWindow) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LegacyProofOfDeliveryWindow> {
        val out = mutableListOf<LegacyProofOfDeliveryWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyProofOfDeliveryWindowStore(): LegacyProofOfDeliveryWindowStore {
    return MemoryLegacyProofOfDeliveryWindowStore(
        mutableMapOf<String, LegacyProofOfDeliveryWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyProofOfDeliveryWindowStore(
    var inner: LegacyProofOfDeliveryWindowStore,
    var cache: TtlCache<LegacyProofOfDeliveryWindow>,
    var hits: Int,
    var misses: Int,
) : LegacyProofOfDeliveryWindowStore {
    override fun find(code: String): LegacyProofOfDeliveryWindow? {
        val hit = (cache.get(code) as LegacyProofOfDeliveryWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyProofOfDeliveryWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyProofOfDeliveryWindow> = inner.list()
}

fun newCachedLegacyProofOfDeliveryWindowStore(inner: LegacyProofOfDeliveryWindowStore, ttl: Int, clock: Clock): LegacyProofOfDeliveryWindowStore {
    return CachedLegacyProofOfDeliveryWindowStore(
        inner = inner,
        cache = newTtlCache<LegacyProofOfDeliveryWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy proof of delivery windows. Empty fields match all. */
data class LegacyProofOfDeliveryWindowQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyProofOfDeliveryWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyProofOfDeliveryWindow>): MutableList<LegacyProofOfDeliveryWindow> {
        val matched = filter(items) { candidate: LegacyProofOfDeliveryWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyProofOfDeliveryWindows(store: LegacyProofOfDeliveryWindowStore, query: LegacyProofOfDeliveryWindowQuery): MutableList<LegacyProofOfDeliveryWindow> {
    return query.apply(store.list())
}
