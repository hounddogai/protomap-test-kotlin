package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt tiers by code. */
interface AttemptTierStore {
    fun find(code: String): AttemptTier?
    fun save(item: AttemptTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptTierStore(
    var items: MutableMap<String, AttemptTier>,
    var order: MutableList<String>,
) : AttemptTierStore {
    override fun find(code: String): AttemptTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptTier) {
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

    override fun list(): MutableList<AttemptTier> {
        val out = mutableListOf<AttemptTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptTierStore(): AttemptTierStore {
    return MemoryAttemptTierStore(mutableMapOf<String, AttemptTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptTierStore(
    var inner: AttemptTierStore,
    var cache: TtlCache<AttemptTier>,
    var hits: Int,
    var misses: Int,
) : AttemptTierStore {
    override fun find(code: String): AttemptTier? {
        val hit = (cache.get(code) as AttemptTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptTier> = inner.list()
}

fun newCachedAttemptTierStore(inner: AttemptTierStore, ttl: Int, clock: Clock): AttemptTierStore {
    return CachedAttemptTierStore(
        inner = inner,
        cache = newTtlCache<AttemptTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt tiers. Empty fields match all. */
data class AttemptTierQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptTier): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptTier>): MutableList<AttemptTier> {
        val matched = filter(items) { candidate: AttemptTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptTiers(store: AttemptTierStore, query: AttemptTierQuery): MutableList<AttemptTier> {
    return query.apply(store.list())
}
