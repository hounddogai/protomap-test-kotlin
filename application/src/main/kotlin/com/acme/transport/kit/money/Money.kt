package com.acme.transport.kit.money

import kotlin.math.abs
import kotlin.math.floor

// Amounts of money in minor units, with their currency.

/** An amount of money. Arithmetic refuses to mix currencies. */
data class Amount(var units: Int, var currency: String) {
    fun plus(other: Amount): Amount {
        if (currency != other.currency) {
            error("cannot add ${other.currency} to $currency")
        }
        return Amount(units + other.units, currency)
    }

    fun minus(other: Amount): Amount {
        if (currency != other.currency) {
            error("cannot subtract ${other.currency} from $currency")
        }
        return Amount(units - other.units, currency)
    }

    fun scaled(factor: Double): Amount {
        val units = floor(this.units.toDouble() * factor + 0.5).toInt()
        return Amount(units, currency)
    }

    fun isZero(): Boolean = units == 0

    fun format(): String {
        val major = units / 100
        val minor = abs(units % 100)
        return "$currency $major.${minor.toString().padStart(2, '0')}"
    }

    fun split(count: Int): MutableList<Amount> {
        val shares = mutableListOf<Amount>()
        if (count <= 0) {
            return shares
        }
        val base = units / count
        val rest = units % count
        for (i in 0 until count) {
            var units = base
            if (i < rest) {
                units += 1
            }
            shares.add(Amount(units, currency))
        }
        return shares
    }
}

fun of(units: Int, currency: String): Amount = Amount(units, currency.trim().uppercase())

fun total(amounts: MutableList<Amount>, currency: String): Amount {
    var sum = of(0, currency)
    for (item in amounts) {
        val next = sum.plus(item)
        sum = next
    }
    return sum
}
