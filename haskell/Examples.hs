module Examples where

import Data.Time
import Data.Time.Clock.System

arbitrary_ex1_e :: [Int] 
arbitrary_ex1_e = take 50 (divByInt one 10)

equation_ex1_e :: [Int]
equation_ex1_e = take 50 (findI (\x -> znorm (mul x half) == half))

equation_ex3_e :: [Int]
equation_ex3_e = take 500 (findI (\x -> znorm (mul x half) == half))

minimise_ex1_e :: [Int]
minimise_ex1_e = take 20 (infimum (\x -> mul x x))

minimise_ex2_e :: [Int]
minimise_ex2_e = take 1000 (infimum (\x -> mul x x))

getTime = do
 t <- getSystemTime
 let nt = systemNanoseconds t
 print (take 1000 (infimum (\x -> mul x x)))
 t2 <- getSystemTime
 let nt2 = systemNanoseconds t2
 return (nt2 - nt)
