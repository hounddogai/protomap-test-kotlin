package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual idling targets by code. */
interface ManualIdlingTargetStore {
    fun find(code: String): ManualIdlingTarget?
    fun save(item: ManualIdlingTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualIdlingTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualIdlingTargetStore(
    var items: MutableMap<String, ManualIdlingTarget>,
    var order: MutableList<String>,
) : ManualIdlingTargetStore {
    override fun find(code: String): ManualIdlingTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualIdlingTarget) {
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

    override fun list(): MutableList<ManualIdlingTarget> {
        val out = mutableListOf<ManualIdlingTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualIdlingTargetStore(): ManualIdlingTargetStore {
    return MemoryManualIdlingTargetStore(mutableMapOf<String, ManualIdlingTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualIdlingTargetStore(
    var inner: ManualIdlingTargetStore,
    var cache: TtlCache<ManualIdlingTarget>,
    var hits: Int,
    var misses: Int,
) : ManualIdlingTargetStore {
    override fun find(code: String): ManualIdlingTarget? {
        val hit = (cache.get(code) as ManualIdlingTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualIdlingTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualIdlingTarget> = inner.list()
}

fun newCachedManualIdlingTargetStore(inner: ManualIdlingTargetStore, ttl: Int, clock: Clock): ManualIdlingTargetStore {
    return CachedManualIdlingTargetStore(
        inner = inner,
        cache = newTtlCache<ManualIdlingTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual idling targets. Empty fields match all. */
data class ManualIdlingTargetQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualIdlingTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualIdlingTarget>): MutableList<ManualIdlingTarget> {
        val matched = filter(items) { candidate: ManualIdlingTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualIdlingTargets(store: ManualIdlingTargetStore, query: ManualIdlingTargetQuery): MutableList<ManualIdlingTarget> {
    return query.apply(store.list())
}
