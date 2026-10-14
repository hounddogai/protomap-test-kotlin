package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal tender exceptions by code. */
interface SeasonalTenderExceptionStore {
    fun find(code: String): SeasonalTenderException?
    fun save(item: SeasonalTenderException)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTenderException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTenderExceptionStore(
    var items: MutableMap<String, SeasonalTenderException>,
    var order: MutableList<String>,
) : SeasonalTenderExceptionStore {
    override fun find(code: String): SeasonalTenderException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTenderException) {
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

    override fun list(): MutableList<SeasonalTenderException> {
        val out = mutableListOf<SeasonalTenderException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTenderExceptionStore(): SeasonalTenderExceptionStore {
    return MemorySeasonalTenderExceptionStore(
        mutableMapOf<String, SeasonalTenderException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTenderExceptionStore(
    var inner: SeasonalTenderExceptionStore,
    var cache: TtlCache<SeasonalTenderException>,
    var hits: Int,
    var misses: Int,
) : SeasonalTenderExceptionStore {
    override fun find(code: String): SeasonalTenderException? {
        val hit = (cache.get(code) as SeasonalTenderException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTenderException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTenderException> = inner.list()
}

fun newCachedSeasonalTenderExceptionStore(inner: SeasonalTenderExceptionStore, ttl: Int, clock: Clock): SeasonalTenderExceptionStore {
    return CachedSeasonalTenderExceptionStore(
        inner = inner,
        cache = newTtlCache<SeasonalTenderException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tender exceptions. Empty fields match all. */
data class SeasonalTenderExceptionQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTenderException): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTenderException>): MutableList<SeasonalTenderException> {
        val matched = filter(items) { candidate: SeasonalTenderException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTenderExceptions(store: SeasonalTenderExceptionStore, query: SeasonalTenderExceptionQuery): MutableList<SeasonalTenderException> {
    return query.apply(store.list())
}
