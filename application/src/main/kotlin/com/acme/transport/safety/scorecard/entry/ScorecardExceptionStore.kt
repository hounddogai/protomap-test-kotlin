package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard exceptions by code. */
interface ScorecardExceptionStore {
    fun find(code: String): ScorecardException?
    fun save(item: ScorecardException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardExceptionStore(
    var items: MutableMap<String, ScorecardException>,
    var order: MutableList<String>,
) : ScorecardExceptionStore {
    override fun find(code: String): ScorecardException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardException) {
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

    override fun list(): MutableList<ScorecardException> {
        val out = mutableListOf<ScorecardException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardExceptionStore(): ScorecardExceptionStore {
    return MemoryScorecardExceptionStore(mutableMapOf<String, ScorecardException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardExceptionStore(
    var inner: ScorecardExceptionStore,
    var cache: TtlCache<ScorecardException>,
    var hits: Int,
    var misses: Int,
) : ScorecardExceptionStore {
    override fun find(code: String): ScorecardException? {
        val hit = (cache.get(code) as ScorecardException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardException> = inner.list()
}

fun newCachedScorecardExceptionStore(inner: ScorecardExceptionStore, ttl: Int, clock: Clock): ScorecardExceptionStore {
    return CachedScorecardExceptionStore(
        inner = inner,
        cache = newTtlCache<ScorecardException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard exceptions. Empty fields match all. */
data class ScorecardExceptionQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardException): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardException>): MutableList<ScorecardException> {
        val matched = filter(items) { candidate: ScorecardException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardExceptions(store: ScorecardExceptionStore, query: ScorecardExceptionQuery): MutableList<ScorecardException> {
    return query.apply(store.list())
}
