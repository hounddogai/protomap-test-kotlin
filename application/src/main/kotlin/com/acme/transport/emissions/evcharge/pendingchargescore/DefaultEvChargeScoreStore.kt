package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default ev charge scores by code. */
interface DefaultEvChargeScoreStore {
    fun find(code: String): DefaultEvChargeScore?
    fun save(item: DefaultEvChargeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultEvChargeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultEvChargeScoreStore(
    var items: MutableMap<String, DefaultEvChargeScore>,
    var order: MutableList<String>,
) : DefaultEvChargeScoreStore {
    override fun find(code: String): DefaultEvChargeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultEvChargeScore) {
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

    override fun list(): MutableList<DefaultEvChargeScore> {
        val out = mutableListOf<DefaultEvChargeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultEvChargeScoreStore(): DefaultEvChargeScoreStore {
    return MemoryDefaultEvChargeScoreStore(
        mutableMapOf<String, DefaultEvChargeScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultEvChargeScoreStore(
    var inner: DefaultEvChargeScoreStore,
    var cache: TtlCache<DefaultEvChargeScore>,
    var hits: Int,
    var misses: Int,
) : DefaultEvChargeScoreStore {
    override fun find(code: String): DefaultEvChargeScore? {
        val hit = (cache.get(code) as DefaultEvChargeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultEvChargeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultEvChargeScore> = inner.list()
}

fun newCachedDefaultEvChargeScoreStore(inner: DefaultEvChargeScoreStore, ttl: Int, clock: Clock): DefaultEvChargeScoreStore {
    return CachedDefaultEvChargeScoreStore(
        inner = inner,
        cache = newTtlCache<DefaultEvChargeScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default ev charge scores. Empty fields match all. */
data class DefaultEvChargeScoreQuery(
    var locale: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultEvChargeScore): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultEvChargeScore>): MutableList<DefaultEvChargeScore> {
        val matched = filter(items) { candidate: DefaultEvChargeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultEvChargeScores(store: DefaultEvChargeScoreStore, query: DefaultEvChargeScoreQuery): MutableList<DefaultEvChargeScore> {
    return query.apply(store.list())
}
